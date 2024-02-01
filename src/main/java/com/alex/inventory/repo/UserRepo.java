package com.alex.inventory.repo;

import com.alex.inventory.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepo extends R2dbcRepository<UserEntity, Long> {

    Mono<UserEntity> findByUsername(String username);

    Mono<Boolean> existsByUsername(String username);


    Mono<Void> deleteUserEntityById(Long id);


    @Modifying
    @Query("UPDATE users SET enabled = :enabled WHERE id = :id")
    Mono<Void> updateEnabled(@Param("id") Long id, @Param("enabled") boolean enabled);

}