package com.example.doumiproject.controller;

import com.example.doumiproject.dto.CommentDto;
import com.example.doumiproject.dto.PostDto;
import com.example.doumiproject.entity.Comment;
import com.example.doumiproject.entity.User;
import com.example.doumiproject.service.CodingTestService;
import com.example.doumiproject.service.CommentService;
import com.example.doumiproject.service.QuizService;
import com.example.doumiproject.service.UserService;

import com.example.doumiproject.util.PaginationUtil;
import com.example.doumiproject.validate.UserValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final QuizService quizService;
    private final CodingTestService codingTestService;
    private final CommentService commentService;

    private final int pageSize = 10;
    @PostMapping("/user/signup")
    public ResponseEntity<?> save(@RequestBody User user, BindingResult bindingResult) {

        UserValidator userValidator = new UserValidator();
        userValidator.validate(user, bindingResult);
        userService.join(user.getUserId(), user.getPassword());

        Map<String, Object> response = new HashMap<>() {{
            put("success", true);
            put("message", "회원가입 성공!");
        }};

        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/login")
    public ResponseEntity<?> login(HttpServletRequest request) {
        //1.회원 정보 조회
        String loginId = request.getParameter("id");
        String loginPassword = request.getParameter("password");
        User user = userService.login(loginId, loginPassword);

        //2. 세션에 회원 정보를 저장 & 세션 유지 시간 설정
        //request에 세션이 있으면 세션을 반환하고, 없으면 신규 세션을 생성해 session에 담는다.
        HttpSession session = request.getSession();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getUserId());
        session.setMaxInactiveInterval(1800);

        //3. 로그인 성공 응답 반환
        Map<String, Object> successResponse = new HashMap<>();
        successResponse.put("success", true);
        successResponse.put("message", "로그인 성공!");
        return ResponseEntity.ok(successResponse);
    }

    @GetMapping("/user/logout")
    public String logout(HttpSession session) {
        session.invalidate(); //세션 무효화
        return "redirect:/";
    }

    @GetMapping("/user/{userId}/mypage")
    public String myPage(@PathVariable("userId") Long userId, HttpSession session, Model model) {

        return "myPage/myPage";
    }

    @GetMapping("/user/{userId}/codingtest/posts")
    public String getCodingTestPost(@PathVariable("userId") Long userId, HttpSession session,
                                    @RequestParam(defaultValue = "1", value = "page") int page, Model model) {

        int totalPages = codingTestService.getTotalPagesForMyPage(userId, "COTE", pageSize);

        List<PostDto> userCoteList = codingTestService.findByUserId(userId, page, pageSize);

        setPaginationAttributes(model, page, totalPages, userId, userCoteList);

        return "myPage/myPageCodingTest";
    }

    @GetMapping("/user/{userId}/quiz/posts")
    public String getQuizPost(@PathVariable("userId") Long userId, HttpSession session,
                              @RequestParam(defaultValue = "1", value = "page") int page, Model model) {

        int totalPages = quizService.getTotalPagesForMyPage(userId, "QUIZ", pageSize);

        List<PostDto> userQuizList = quizService.findByUserId(userId, page, pageSize);

        setPaginationAttributes(model, page, totalPages, userId, userQuizList);

        return "myPage/myPageQuiz";
    }

    @GetMapping("/user/{userId}/comment/posts")
    public String getCommentPost(@PathVariable("userId") Long userId, HttpSession session,
                                 @RequestParam(defaultValue = "1", value = "page") int page, Model model) {

        int totalPages = commentService.getTotalPagesForMyPage(userId, pageSize);

        List<CommentDto> userCommentList = commentService.getCommentList(userId, page, pageSize);

        setPaginationAttributes(model, page, totalPages, userId, userCommentList);

        return "myPage/myPageComment";
    }

    private void setPaginationAttributes(Model model, int page, int totalPages, Long userId, List<?> contents) {

        if (page < 1) {
            page = 1;
        }

        int startIdx = PaginationUtil.calculateStartIndex(page);
        int endIdx = PaginationUtil.calculateEndIndex(page, totalPages);

        model.addAttribute("contents", contents);
        model.addAttribute("currentPage", page);
        model.addAttribute("startIdx", startIdx);
        model.addAttribute("endIdx", endIdx);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("userId", userId);
    }
}
