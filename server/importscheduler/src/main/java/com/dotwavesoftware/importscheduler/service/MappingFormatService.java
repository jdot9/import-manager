package com.dotwavesoftware.importscheduler.service;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.dotwavesoftware.importscheduler.entity.MappingFormatEntity;
import com.dotwavesoftware.importscheduler.repository.MappingFormatRepository;



@Service
public class MappingFormatService {
    
    private static final Logger logger = Logger.getLogger(MappingFormatService.class.getName());
    private final MappingFormatRepository mappingFormatRepository;

    public MappingFormatService(MappingFormatRepository mappingFormatRepository) {
        this.mappingFormatRepository = mappingFormatRepository;
    }

    public List<MappingFormatEntity> getAllFormats() {
        logger.info("Returning all mapping formats.");
        return mappingFormatRepository.findAll();
    }

}
