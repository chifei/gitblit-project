/*
 * Copyright 2011 gitblit.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gitblit;

import com.gitblit.console.ConsoleContext;
import com.gitblit.servlet.GitblitContext;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * GitBlitServer is the embedded Jetty server for Gitblit GO. This class starts
 * and stops an instance of Jetty that is configured from a combination of the
 * gitblit.properties file and command line parameters. JCommander is used to
 * simplify command line parameter processing. This class also automatically
 * generates a self-signed certificate for localhost, if the keystore does not
 * already exist.
 *
 * @author James Moger
 */
public class ConsoleServer extends GitBlitServer {

    public static void main(String... args) {
        GitBlitServer server = new ConsoleServer();
        List<String> filtered = new ArrayList();
        String folder = "data";

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.equals("--baseFolder")) {
                if (i + 1 == args.length) {
                    System.out.println("Invalid --baseFolder parameter!");
                    System.exit(-1);
                } else if (!".".equals(args[i + 1])) {
                    folder = args[i + 1];
                }

                ++i;
            } else {
                filtered.add(arg);
            }
        }

        Params.baseFolder = folder;
        Params params = new Params();
        CmdLineParser parser = new CmdLineParser(params);

        try {
            parser.parseArgument(filtered);
            if (params.help) {
                server.usage(parser, (CmdLineException) null);
            }
        } catch (CmdLineException var7) {
            server.usage(parser, var7);
        }

        if (params.stop) {
            server.stop(params);
        } else {
            server.start(params);
        }

    }

    protected GitblitContext newGitblit(IStoredSettings settings, File baseFolder) {
        return new ConsoleContext(settings, baseFolder);
    }
}
