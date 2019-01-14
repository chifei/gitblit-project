<img src="doc/img/jweb-logo.png" align="left" />

# JWeb Framework

A developer friendly module based JAX-RS micro service framework. 

- **Built for startup, provides well designed quick prototyping code base.**
- Supports template editing, perfect for building landing pages.
- JAX-RS module framework
  * Enhancements to Jersey
  * Supports Guice style DI/AOP 
  * Embedded HTTP server with undertow

## Getting Started

These instructions will get you a copy of the JWeb CMS up and running on your local machine.<br>

### Prerequisites

1. Download and install [Open JDK 11](http://jdk.java.net/11/) or [Oracle JDK 11](http://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-4416644.html)
2. If you want to use MySQL as database. (***Optional, JWeb default embeds HSQL***)
   1. Download and install [MySQL](https://dev.mysql.com/downloads/mysql/). 
   2. Create a database. <br>
   ```CREATE DATABASE main CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;```
   3. Create a database user with schema update permission. <br>
   ```   CREATE USER 'user'@'localhost' IDENTIFIED BY 'password';   GRANT ALL PRIVILEGES ON * . * TO 'user'@'localhost';   FLUSH PRIVILEGES;   ```

### Installing

1. Download the package file [JWeb-v0.9.0-beta.zip](https://github.com/JWeb-io/JWeb-project/releases/download/v0.9.0/JWeb-v0.9.0-beta.zip) (***For all platform***)
2. Unzip the package
3. Run `./bin/JWeb`
4. Use a browser(IE11+) to open ```http://localhost:8080```
5. Fill in the require information to setup JWeb. 
   1. Choose language. 
   2. Input app name, the name will be displayed in page title. 
   3. Select database. 
      1. If you want to use MySQL, input the info of database and user you created.
   4. Input SMTP settings. 
      > Optional, if you skip the SMTP settings, user register will be disabled.
   5. Click install button. JWeb will restart.
6. Open ```http://localhost:8080/admin/```


## Run Source Code

1. Clone the repo
2. Import as a Gradle project to Intellij IDEA or Eclipse.
3. Run JWeb-main/src/java/Main.java 

### Known issues

* There will be errors in module-info.java for duplicate module java.xml.bind
* There will be errors in DAO related source codes for missing @Transactional. 

It is because of Java 10 JEE split package issues. A temp fix for Intellij IDEA is: 
1. Open settings. Build, Execution, Deployment>Compiler>Java Compiler. 
2. Input the following parameters to Additional command line parameters. And replace the path of javax.transaction-api-1.3.jar. 
   ```
   --add-modules=java.xml.bind --patch-module java.transaction=~\.gradle\caches\modules-2\files-2.1\javax.transaction\javax.transaction-api\1.3\e006adf5cf3cca2181d16bd640ecb80148ec0fce\javax.transaction-api-1.3.jar
   ```
3. The error messages are still there, but it compiles. 

## Code Examples

To start an `App`: 

```
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Path dir = Paths.get(System.getProperty("user.home")).resolve(".JWeb");
        App app = new UndertowApp(dir);
        ServiceLoader.load(AbstractModule.class).forEach(app::install);
        app.start();
    }
}
```

To create a `Module`:
```

public class TodoServiceModuleImpl extends TodoServiceModule {
    @Override
    protected void configure() {
        //import DatabaseModule to register entity and create repository
        module(DatabaseModule.class)
            .entity(Task.class);
        
        bind(TaskService.class);
        
        //register service implementation
        api().service(TaskWebService.class, TaskWebServiceImpl.class);
    }
}
```


## Release Notes

* 0.9.1 
  * Clear modules, remove page search/rss and edm features.
  * Add GA/addthis modules

* 0.9.0
  * User management, supports login/register/forget password/pincode/captcha code.
  * Page management, supports category/page/template/variable/components.
  * File management, supports upload files, download files, scale images, react file browser. 
  * Basic email template support

## Authors

* Chi
  > If you need support for customization or commerce license, please feel free to contact me ``chiron.chi#gmail.com``

See also the list of [contributors](https://github.com/your/project/contributors) who participated in this project.

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.


## License

This project is licensed under the AGPL License - see the [LICENSE.md](LICENSE.md) file for details


