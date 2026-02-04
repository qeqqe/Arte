package com.arte.jobhunter.feature.jobscraper;

import com.arte.jobhunter.dto.FetchJobRequest;
import com.arte.jobhunter.dto.SearchResponse;
import com.arte.jobhunter.grpc.FetchJobResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JobScraperHandler {

    private final SearxngHelper searxngHelper;

    public JobScraperHandler(SearxngHelper searxngHelper) {
        this.searxngHelper = searxngHelper;
    }


    public FetchJobResponse scrapeJob(FetchJobRequest request) {
        try {
            List<String> queries = new ArrayList<>();
            for(String skill : request.skills()) {
                queries.add(String.format("%s developer jobs in %s linkedin", skill, request.location()));
            }

            List<String> links = new ArrayList<>();

            for(String query : queries) {
                SearchResponse response = searxngHelper.search(query);
                links.add(response.result().getFirst().url());
            }
        } catch (Exception e) {
            log.error("Error scraping jobs for request: {}", request, e);
            return FetchJobResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Error scraping jobs: " + e.getMessage())
                    .build();
        }
    }

}


/// reference
/// when you fetch the page from searxng
///          ↓ this shit
//<ul class="jobs-search__results-list">
//   <li>
///                                             ↓ Job id
//      <div data-entity-urn="urn:li:jobPosting:{JOBID}">
///                     ↓ or this direct url
//        <a href="https://in.linkedin.com/jobs/view/some-mobile-viewbs-{JOBID}">
//          ...
//    </li>
//</ul>