package com.kensbunker.micronaut;

import com.kensbunker.micronaut.auth.persistence.UserEntity;
import com.kensbunker.micronaut.auth.persistence.UserRepository;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.event.annotation.EventListener;
import javax.inject.Singleton;

@Singleton
public class TestDataProvider {

  private final UserRepository users;


  public TestDataProvider(UserRepository users) {
    this.users = users;
  }

  @EventListener
  public void init(StartupEvent event) {
    String email = "alice@example.com";
    if (!users.findByEmail(email).isPresent()) {
      UserEntity alice = new UserEntity();
      alice.setEmail(email);
      alice.setPassword("secret");
      users.save(alice);
    }
  }
}
