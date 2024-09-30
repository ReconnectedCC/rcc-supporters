package cc.reconnected;

import io.wispforest.owo.config.annotation.Config;

@Config(name = "rcc-supporter", wrapperName = "RccSupporterConfig")
public class ConfigModel {
    public String jdbcUrl = "jdbc:postgresql://127.0.0.1:5432/rcc?user=myuser&password=mypassword";
}
