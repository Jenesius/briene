package com.salat.briene.controllers;

import com.salat.briene.payload.response.AuthorDTO;
import com.salat.briene.entities.Article;
import com.salat.briene.entities.User;
import com.salat.briene.exceptions.IllegalArticleStateException;
import com.salat.briene.exceptions.UserNotFoundException;
import com.salat.briene.services.ArticleService;
import com.salat.briene.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final ArticleService articleService;
    private final UserService userService;

    @GetMapping("/{authorName}")
    public ResponseEntity<AuthorDTO> getAuthorPage(@PathVariable String authorName)
            throws UserNotFoundException, IllegalArticleStateException {
        User userAuthor = userService.loadUserByUsername(authorName);
        List<Article> articles = articleService.getArticlesByAuthorAndState(userAuthor, "published");

        AuthorDTO author = new AuthorDTO(userAuthor, articles);
        return ResponseEntity.ok().body(author);
    }
}