package com.dotwavesoftware.importscheduler.controller;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.service.MappingFormatService;



@RestController
@RequestMapping("/api")
public class MappingController {
   
    private static final Logger logger = Logger.getLogger(MappingController.class.getName());
    private final MappingFormatService mappingFormatService;
    
    public MappingController(MappingFormatService mappingFormatService) {
        this.mappingFormatService = mappingFormatService;
    }

    @GetMapping("/mapping-formats")
    public ResponseEntity<List<MappingFormatEntity>> getAllFormats() {
        logger.info("Request to get all mapping formats");
        List<MappingFormatEntity> formats = mappingFormatService.getAllFormats();
        return ResponseEntity.ok(formats);
    }

}
