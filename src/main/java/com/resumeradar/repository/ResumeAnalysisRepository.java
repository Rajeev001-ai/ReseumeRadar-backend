package com.resumeradar.repository;

import com.resumeradar.entity.ResumeAnalysis;
import com.resumeradar.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {

	List<ResumeAnalysis> findByUserIdOrderByCreatedAtDesc(Long userId);

	List<ResumeAnalysis> findByUserOrderByCreatedAtDesc(User user);

	Optional<ResumeAnalysis> findByIdAndUser(Long id, User user);

	List<ResumeAnalysis> findAllByOrderByCreatedAtDesc();

	long countByUser(User user);

	long countByAtsScoreBetween(Integer minScore, Integer maxScore);

	long countByAtsScoreLessThan(Integer maxScore);
}
