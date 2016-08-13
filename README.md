play-logger
===========

Play framework 1.4.x module for enterprise-grade request logging.

Features
--------

- Logs all requests with parameters, session id and response time
- Generates unique request id to be used in other log files for easy matching of other log messages (eg errors) with requests
- Provides Log4j ExtendedPatternLayout, supporting the following new patterns:
  - %R - current request id
  - %h - current Java Heap size

Add it to your dependencies.yml
-------------------------------

```
require:
    - play
    - play-codeborne -> logger 1.15

repositories:
    - play-logger:
          type: http
          artifact: https://repo.codeborne.com/play-logger/[module]-[revision].zip
          contains:
              - play-codeborne -> *
```

Use it in your log4j.xml configuration
--------------------------------------
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
    <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/">
      <appender name="request" class="org.apache.log4j.DailyRollingFileAppender">
        <param name="file" value="logs/request.log"/>
        <layout class="play.modules.logger.ExtendedPatternLayout">
          <param name="ConversionPattern" value="%d{yyyy.MM.dd HH:mm:ss,SSS Z} %h [%R] %m%n"/>
        </layout>
      </appender>

      <appender name="general" class="play.modules.logger.PlayPreprocessingRollingFileAppender">
        <param name="file" value="logs/general.log"/>
        <layout class="play.modules.logger.ExtendedPatternLayout">
          <param name="ConversionPattern" value="%d{yyyy.MM.dd HH:mm:ss,SSS Z} [%R] %-5p %c{1} - %m%n"/>
        </layout>
      </appender>

      <logger name="request" additivity="false">
        <appender-ref ref="request"/>
      </logger>

      <root>
        <level value="INFO"/>
        <appender-ref ref="general"/>
      </root>
    </log4j:configuration>
