package com.devteria.profile.repository;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.devteria.profile.entity.UserProfile;

public interface UserProfileRepository extends Neo4jRepository<UserProfile, String> {}
