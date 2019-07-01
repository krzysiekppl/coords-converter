package com.tomtom.coordinates_converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ConverterContoller {

    @Autowired
    private Converter converter;

    @GetMapping(value = "/")
    private String showInputPages() {
        return "index";
    }

    @PostMapping(value = "/convert_coordinates")
    private String convertCoordinates(Model model,
                                      @ModelAttribute(name = "coordinates") String coord,
                                      @ModelAttribute(name = "order") String order) {
        converter.checkInputCoordinates(coord, order);

        model.addAttribute("original", converter);
        return "index";
    }
}
