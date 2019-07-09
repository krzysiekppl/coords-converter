package com.tomtom.coordinates_converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class ConverterContoller {

    @Autowired
    private Converter converter;

    @GetMapping(value = "/")
    private String showInputPages(Model model) {
        model.addAttribute("message", "Wprowadź koordynaty aby przekonwertować je na wybrane formaty.");
        return "index";
    }

    @PostMapping(value = "/")
    private String convertCoordinates(Model model,
                                      @ModelAttribute(name = "coordinates") String coord,
                                      @ModelAttribute(name = "order") String order,
                                      HttpServletResponse response) throws IOException {
        try {
            converter.convertCoordinates(coord, order);
        }catch (Exception e){
            model.addAttribute("message"," (Sprawdź poprawność wprowadzonych wartości.)");
            model.addAttribute("original", converter);
            return "index";
        }
        model.addAttribute("message", "Uzyskane wyniki");
        model.addAttribute("original", converter);
        return "index";
    }
}
