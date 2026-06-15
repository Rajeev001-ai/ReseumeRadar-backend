package com.resumeradar.repository;

import com.resumeradar.entity.MatchedKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchedKeywordRepository extends JpaRepository<MatchedKeyword, Long> {

	List<MatchedKeyword> findByResumeAnalysisId(Long resumeAnalysisId);
}
