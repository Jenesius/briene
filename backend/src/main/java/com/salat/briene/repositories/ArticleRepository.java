package com.salat.briene.repositories;

import com.salat.briene.entities.Article;
import com.salat.briene.entities.ArticleState;
import com.salat.briene.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // Optional<Article> findArticleByTitle(String title);

    Optional<Article> findArticleByTitleAndState(String title, ArticleState state);

    List<Article> findArticlesByState(ArticleState state);

    List<Article> findArticlesByAuthor(User author);

    List<Article> findArticlesByAuthorAndState(User author, ArticleState state);
}