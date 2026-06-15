package com.resumeradar.repository;

import com.resumeradar.entity.MissingKeyword;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissingKeywordRepository extends JpaRepository<MissingKeyword, Long> {

	List<MissingKeyword> findByResumeAnalysisId(Long resumeAnalysisId);
}
