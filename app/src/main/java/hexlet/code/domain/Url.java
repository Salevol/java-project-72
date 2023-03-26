package hexlet.code.domain;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.net.URL;
import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@Entity
public final class Url extends Model {

    @Id
    private long id;

    private String name;

    @WhenCreated
    private Instant createdAt;

    Url(String name) {
        this.name = name;
    }

    public Url(URL url) {
        this.name = url.getPort() == -1
                ? url.getProtocol() + "://" + url.getHost()
                : url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
    }


}
