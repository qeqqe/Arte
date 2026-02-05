package com.arte.jobhunter.feature.jobscraper;

import com.arte.jobhunter.dto.FetchJobRequest;
import com.arte.jobhunter.dto.SearchResponse;
import com.arte.jobhunter.grpc.FetchJobResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class JobScraperHandler {

    private final SearxngHelper searxngHelper;
    private final HttpClient httpClient;

    public JobScraperHandler(SearxngHelper searxngHelper) {
        this.searxngHelper = searxngHelper;
		this.httpClient = HttpClient.newBuilder()
                .build();
	}

    /**
     * Takes user desired work location and skill-set and returns job offer.
     * Scrapes the HTML class 'jobs-search__results-list' from the job listing
     * and returns top 1-2 results per skill
     * @param request - FetchJobRequest, takes location in string and Skills in List<string>
     * @return FetchJobResponse
     */
    public FetchJobResponse scrapeJob(FetchJobRequest request) {
        try {
            List<String> queries = new ArrayList<>();
            for(String skill : request.skills()) {
                queries.add(String.format("%s developer jobs in %s linkedin", skill, request.location()));
            }

            List<String> links = new ArrayList<>();

            List<URI> jobLinks = getJobLink(links);

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

    private List<URI> getJobLink(List<String> jobListLink) {
        try {
            List<URI> jobLink = new ArrayList<>();

            jobListLink
                    .forEach(link -> {
						try {
							Document doc = Jsoup.connect(link)
									.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
									.get();
                            Element jobSearchResult = doc.selectFirst(".jobs-search__results-list");
                            if(jobSearchResult == null || !jobSearchResult.children().hasText()) {
                                log.warn("No job search list element found :/");
                                return;
                            }

                            Elements jobSearchResultList = jobSearchResult.children();

                            jobSearchResultList.stream()
                                    .limit(2)
                                    .forEach(list -> {
                                        Element div = list.firstElementChild();

                                        String urn = div.attr("data-entity-urn");
                                        String[] parts = urn.split(":");

                                        String jobId = parts[parts.length - 1];
                                        jobLink.add(URI.create("https://www.linkedin.com/jobs/view/" + jobId));
                                    });
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					});

            return jobLink;

        } catch (Exception e) {
            log.error("Error searching for jobs list for request: ", e);
            return null;
        }
    }

}


/// reference
/// when you fetch the page from searxng
///          ↓ this shit
//<ul class="jobs-search__results-list">
//   <li>
///      first child                            ↓ Job id
//      <div data-entity-urn="urn:li:jobPosting:{JOBID}">
///                     ↓ or this direct url
//        <a href="https://in.linkedin.com/jobs/view/some-mobile-viewbs-{JOBID}">
//          ...
//    </li>
//</ul>