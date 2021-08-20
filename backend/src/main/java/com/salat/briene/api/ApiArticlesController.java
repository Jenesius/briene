package com.salat.briene.api;

import com.salat.briene.entities.Article;
import com.salat.briene.entities.ArticleState;
import com.salat.briene.exceptions.ArticleNotFoundException;
import com.salat.briene.exceptions.IllegalArticleStateException;
import com.salat.briene.services.ArticleService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin("http://localhost:8081")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiArticlesController {
    private final ArticleService articleService;

    @GetMapping("/articles")
    public ResponseEntity<?> getArticles() {
        @Getter
        class ArticleContainer {
            private final Long id;
            private final String title;

            public ArticleContainer(Article article) {
                this.id = article.getId();
                this.title = article.getTitle();
            }
        }

        try {
            List<ArticleContainer> publishedArticles = articleService.getArticlesByState("published")
                    .stream().map(ArticleContainer::new)
                    .collect(Collectors.toList());

            return ResponseEntity.ok().body(publishedArticles);
        } catch (IllegalArticleStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/articles/{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {
        @Getter
        class ArticleContainer {
            private final Long id;
            private final String title;
            private final String author;
            private final String htmlContent;
            private final Date publicationDate;

            public ArticleContainer(Article article) {
                this.id = article.getId();
                this.title = article.getTitle();
                this.author = article.getAuthor().getUsername();
                this.htmlContent = article.makeHTML();
                this.publicationDate = article.getPublicationDate();
            }
        }

        try {
            Article article = articleService.getArticleById(id);

            if (article.getState().equals(ArticleState.ARTICLE_IN_EDITING)) {
                throw new ArticleNotFoundException();
            } // todo replace if user can see article

            return ResponseEntity.ok().body(new ArticleContainer(article));
        } catch (ArticleNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
