package org.lunaratlas.astronomy;

import java.time.Instant;
import java.util.Optional;

public record HorizonTimes(Optional<Instant> moonrise, Optional<Instant> moonset) {
}
