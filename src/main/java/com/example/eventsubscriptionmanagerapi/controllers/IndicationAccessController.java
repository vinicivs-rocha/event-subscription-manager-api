package com.example.eventsubscriptionmanagerapi.controllers;

import com.example.eventsubscriptionmanagerapi.dtos.ErrorMessageDTO;
import com.example.eventsubscriptionmanagerapi.dtos.IndicationAccessSavingDTO;
import com.example.eventsubscriptionmanagerapi.exceptions.EventNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotFoundException;
import com.example.eventsubscriptionmanagerapi.exceptions.IndicatorNotSubscribedToEvent;
import com.example.eventsubscriptionmanagerapi.services.IndicationAccessService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/indication-accesses")
public class IndicationAccessController {
    private final IndicationAccessService indicationAccessService;

    public IndicationAccessController(IndicationAccessService indicationAccessService) {
        this.indicationAccessService = indicationAccessService;
    }

    @PostMapping
    public ResponseEntity<Object> save(@RequestBody IndicationAccessSavingDTO indicationAccessSavingDTO) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(indicationAccessService.save(indicationAccessSavingDTO));
        } catch (IndicatorNotFoundException | EventNotFoundException | IndicatorNotSubscribedToEvent e) {
            return ResponseEntity.status(400).body(new ErrorMessageDTO(e.getMessage()));
        }
    }
}
