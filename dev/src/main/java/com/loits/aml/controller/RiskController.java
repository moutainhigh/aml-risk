package com.loits.aml.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

/**
 * Managing Riskrelated operations
 *
 * @author Minoli De Silva - Infinitum360
 * @version 1.0.0
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/v1")
@SuppressWarnings("unchecked")
public class RiskController {

    /**
     * Risk calculation
     *
     */

    @GetMapping(produces = "application/json")
    public @ResponseBody
    void  getRisk() {

    }
}
