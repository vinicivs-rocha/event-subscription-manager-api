package com.example.eventsubscriptionmanagerapi.repositories;

import com.example.eventsubscriptionmanagerapi.models.IndicationAccess;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface IndicationAccessRepository extends CrudRepository<IndicationAccess, UUID> {
}
