package com.kensbunker.micronaut.auth;

import com.kensbunker.micronaut.auth.persistence.UserEntity;
import com.kensbunker.micronaut.auth.persistence.UserRepository;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationException;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.UserDetails;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class JDBCAuthenticationProvider implements AuthenticationProvider {

  private static final Logger LOG = LoggerFactory.getLogger(JDBCAuthenticationProvider.class);
  private final UserRepository users;

  public JDBCAuthenticationProvider(UserRepository users) {
    this.users = users;
  }

  @Override
  public Publisher<AuthenticationResponse> authenticate(
      @Nullable HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
    final String identity = (String) authenticationRequest.getIdentity();
    LOG.debug("User {} tries to login...", identity);
    return Flowable.create(
        emitter -> {
          Optional<UserEntity> maybeUser = users.findByEmail(identity);
          if (maybeUser.isPresent()) {
            LOG.debug("Found user: {}", maybeUser.get().getEmail());
            String secret = (String) authenticationRequest.getSecret();
            if (maybeUser.get().getPassword().equals(secret)) {
              // pass
              LOG.debug("User logged in.");
              final HashMap<String, Object> attributes = new HashMap<>();
              attributes.put("hair_color", "brown");
              attributes.put("language", "en");
              emitter.onNext(
                  new UserDetails(identity, Collections.singletonList("ROLE_USER"), attributes));
              return;
            } else {
              LOG.debug("Wrong password provided for user {}", identity);
            }
          } else {
            LOG.debug("No user found with email: {}", identity);
          }
          emitter.onError(
              new AuthenticationException(new AuthenticationFailed("Wrong username or password")));
        },
        BackpressureStrategy.ERROR);
  }
}
