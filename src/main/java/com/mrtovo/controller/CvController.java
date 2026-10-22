package com.mrtovo.controller;

import com.mrtovo.repository.CvRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Registre des CV déposés : candidature liée, employé RH qui a traité le CV et date. */
@Controller
@RequestMapping("/rh/cvs")
@RequiredArgsConstructor
public class CvController {

    private final CvRepository cvRepository;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("cvs", cvRepository.findTousAvecCandidature());
        return "cv/liste";
    }
}
