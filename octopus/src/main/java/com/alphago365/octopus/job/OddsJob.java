package com.alphago365.octopus.job;


import com.alphago365.octopus.PriorityJobScheduler;
import com.alphago365.octopus.exception.ParseException;
import com.alphago365.octopus.model.Match;
import com.alphago365.octopus.model.Odds;
import com.alphago365.octopus.parser.OddsParser;
import com.alphago365.octopus.service.OddsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Scope("prototype")
@Component
@Slf4j
public class OddsJob extends MatchRelatedJob {

    @Autowired
    private OddsService oddsService;

    @Autowired
    PriorityJobScheduler priorityJobScheduler;

    public OddsJob(long delay, Match match) {
        super(String.format("O-%d", match.getId()), delay, match);
    }

    @Override
    public void runJob() {
        AtomicLong sum = new AtomicLong(0);
        save(parse(download())).forEach(odds -> {
            sum.getAndAdd(downloadConfig.getOddsChangeDelay());
            OddsChangeJob oddsChangeJob = applicationContext.getBean(OddsChangeJob.class, sum.get(), odds);
            priorityJobScheduler.scheduleJob(oddsChangeJob);
        });
    }

    private List<Odds> save(List<Odds> oddsList) {
        return oddsService.saveAll(oddsList);
    }

    private List<Odds> parse(String json) {
        try {
            OddsParser oddsParser = applicationContext.getBean(OddsParser.class, match);
            return oddsParser.parseResponse(json);
        } catch (ParseException e) {
            log.error("Download odds error", e);
        }
        return Collections.emptyList();
    }

    private String download() {
        String url = downloadConfig.getOddsUrl()
                .replaceFirst("MATCH_ID_PLACEHOLDER", String.valueOf(match.getId()));
        return restService.getJson(url);
    }
}
