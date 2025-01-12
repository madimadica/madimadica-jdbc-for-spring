package com.madimadica.jdbc.web;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Root configuration source of the library.
 * Include via <code>@Import(InitializeJdbc.class)</code>
 */
@Configuration
@ComponentScan("com.madimadica.jdbc.web")
public class InitializeJdbc {
    /**
     * Visible constructor for Spring initialization
     */
    public InitializeJdbc() {}
}
