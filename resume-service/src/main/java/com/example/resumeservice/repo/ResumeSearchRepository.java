package com.example.resumeservice.repo;


import com.example.resumeservice.entity.ResumeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeSearchRepository extends ElasticsearchRepository<ResumeDocument, String> {
    @Query("{\"match\": {\"content\": {\"query\": \"?0\", \"operator\": \"and\"}}}")
    List<ResumeDocument> searchByContent(String keyword);
}
