package com.alphago365.octopus.model;

import com.alphago365.octopus.model.audit.DateAudit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "handicaps")
@Data
@NoArgsConstructor
public class Handicap extends DateAudit {

    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Provider provider;

    private int displayOrder;

    @OneToMany(mappedBy = "handicap", cascade = CascadeType.ALL)
    private List<HandicapChange> changeHistories;

    @OneToMany(mappedBy = "handicap", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL)
    private List<HandicapAnalysis> changeAnalyses;

    private double ratioHome;
    private double ratioAway;

    private double kellyHome;
    private double kellyAway;

    private double payoff;

    public Handicap(Match match) {
        this.match = match;
    }

    public String toString() {
        return match.toString() + " " + provider.toString();
    }
}
