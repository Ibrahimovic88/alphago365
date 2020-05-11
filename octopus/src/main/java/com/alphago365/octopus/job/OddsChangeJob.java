package com.alphago365.octopus.job;


import com.alphago365.octopus.exception.ParseException;
import com.alphago365.octopus.model.Odds;
import com.alphago365.octopus.model.OddsChange;
import com.alphago365.octopus.parser.OddsChangeParser;
import com.alphago365.octopus.service.OddsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Scope("prototype")
@Component
@Slf4j
public class OddsChangeJob extends OddsRelatedJob {

    @Autowired
    private OddsService oddsService;

    public OddsChangeJob(long delay, Odds odds) {
        super("OCJ", delay, odds);
    }

    @Override
    public void runJob() {
        save(parse(download()));
    }

    private List<OddsChange> save(List<OddsChange> handicapChangeList) {
        return oddsService.saveAllChanges(handicapChangeList);
    }

    private List<OddsChange> parse(String json) {
        try {
            OddsChangeParser oddsChangeParser = applicationContext.getBean(OddsChangeParser.class, odds);
            return oddsChangeParser.parseResponse(json);
        } catch (ParseException e) {
            log.error("Download odds error", e);
        }
        return Collections.emptyList();
    }

    private String download() {
        Long matchId = odds.getMatch().getId();
        Integer providerId = odds.getProvider().getId();
        String url = downloadConfig.getOddsChangeUrl()
                .replaceFirst("MATCH_ID_PLACEHOLDER", String.valueOf(matchId)) // first
                .replaceFirst("PROVIDER_ID_PLACEHOLDER", String.valueOf(providerId)); // second
        return restService.getJson(url);
    }
}
