package com.saimone.bvp_software_task.repository;

import com.saimone.bvp_software_task.model.ConfirmToken;
import com.saimone.bvp_software_task.model.TokenAssignment;
import com.saimone.bvp_software_task.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConfirmTokenRepository extends JpaRepository<ConfirmToken, Long> {
    Optional<ConfirmToken> findByToken(String token);

    @Query("SELECT ct.user FROM ConfirmToken ct WHERE ct.token = :token")
    Optional<User> findUserByConfirmToken(@Param("token") String token);

    @Query(value = """
      select t from ConfirmToken t inner join User u\s
      on t.user.id = u.id\s
      where u.id = :id and (t.expired = false or t.revoked = false)\s
      """)
    List<ConfirmToken> findAllValidConfirmTokenByUser(Long id);

    List<ConfirmToken> findByUserAndTokenAssignmentAndExpiredAndRevoked(User user, TokenAssignment tokenAssignment, Boolean expired, Boolean revoked);

    List<ConfirmToken> findByUserAndTokenAssignment(User user, TokenAssignment tokenAssignment);
}