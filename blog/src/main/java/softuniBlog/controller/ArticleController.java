package softuniBlog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import softuniBlog.bindingModel.ArticleBindingModel;
import softuniBlog.entity.Article;
import softuniBlog.entity.User;
import softuniBlog.repository.ArticleRepository;
import softuniBlog.repository.UserRepository;

@Controller
public class ArticleController {

	@Autowired
	private ArticleRepository articleRepository;
	@Autowired
	private UserRepository userRepository;

	@GetMapping("/article/create")
	@PreAuthorize("isAuthenticated()")
	public String create(Model model) {
		model.addAttribute("view", "article/create");

		return "base-layout";
	}

	@PostMapping("/article/create")
	@PreAuthorize("isAuthenticated()")
	public String createProcess(ArticleBindingModel articleBindingModel) {
		UserDetails user = (UserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User userEntity = this.userRepository.findByEmail(user.getUsername());

		Article articleEntity = new Article(
				articleBindingModel.getTitle(),
				articleBindingModel.getContent(),
				userEntity
		);

		this.articleRepository.saveAndFlush(articleEntity);

		return "redirect:/";
	}

	@GetMapping("/article/{id}")
	public String details(Model model, @PathVariable Integer id) {
		if (!this.articleRepository.exists(id)) {
			return "redirect:/";
		}

		if (!(SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken)){
			UserDetails user = (UserDetails) SecurityContextHolder.getContext()
					.getAuthentication().getPrincipal();

			User userEntity = this.userRepository.findByEmail(user.getUsername());

			model.addAttribute("user", userEntity);
		}

		Article article = this.articleRepository.findOne(id);

		model.addAttribute("article", article);
		model.addAttribute("view", "article/details");

		return "base-layout";
	}

	@GetMapping("/article/edit/{id}")
	@PreAuthorize("isAuthenticated()")
	public String edit(@PathVariable Integer id, Model model) {
		if (!this.articleRepository.exists(id)){
			return "redirect:/";
		}

		Article article = this.articleRepository.findOne(id);

		if (!this.isUserAuthorOrAdmin(article)) {
			return "redirect:/";
		}

		model.addAttribute("article", article);
		model.addAttribute("view", "article/edit");

		return "base-layout";
	}

	@PostMapping("/article/edit/{id}")
	@PreAuthorize("isAuthenticated()")
	public String editProcess(@PathVariable Integer id, ArticleBindingModel model) {
		if (!this.articleRepository.exists(id)){
			return "redirect:/";
		}

		Article article = this.articleRepository.findOne(id);

		if (!this.isUserAuthorOrAdmin(article)) {
			return "redirect:/";
		}

		article.setTitle(model.getTitle());
		article.setContent(model.getContent());

		this.articleRepository.saveAndFlush(article);

		return "redirect:/article/" + article.getId();
	}

	private boolean isUserAuthorOrAdmin(Article article) {
		UserDetails user = (UserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal();

		User userEntity = this.userRepository.findByEmail(user.getUsername());

		return  userEntity.isAdmin() || userEntity.isAuthor(article);
	}

	@GetMapping("/pages/about")
	public String openAbout(Model model) {
		model.addAttribute("view", "pages/about");

		return "base-layout";
	}

}
