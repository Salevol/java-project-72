package hexlet.code.domain;


import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import java.time.Instant;


@Getter
@Setter
@RequiredArgsConstructor
@Entity
public class UrlCheck extends Model {
    @Id
    private long id;

    private final int statusCode;

    private final String title;

    private final String h1;

    @Lob
    private final String description;

    @WhenCreated
    private Instant createdAt;

    @ManyToOne(optional = false)
    private final Url url;
}
