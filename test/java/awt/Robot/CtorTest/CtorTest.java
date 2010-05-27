/*
 * Copyright (c) 2009 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
  @test
  @bug 6855323
  @summary  Robot(GraphicsDevice) constructor initializes LEGAL_BUTTON_MASK variable improperly
  @author Dmitry Cherepanov area=awt.robot
  @run main CtorTest
*/

/**
 * CtorRobot.java
 *
 * summary: creates Robot using one parameter constructor
 */

import java.awt.*;
import java.awt.event.*;

import sun.awt.SunToolkit;

public class CtorTest
{
    public static void main(String []s) throws Exception
    {
        // one parameter constructor
        GraphicsDevice graphicsDevice = GraphicsEnvironment.
            getLocalGraphicsEnvironment().getDefaultScreenDevice();
        Robot robot = new Robot(graphicsDevice);
        clickOnFrame(robot);
    }

    // generate mouse events
    private static void clickOnFrame(Robot robot) {
        Frame frame = new Frame();
        frame.setBounds(100, 100, 100, 100);
        frame.setVisible(true);

        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();

        // click in the middle of the frame
        robot.mouseMove(150, 150);
        robot.delay(50);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);

        ((SunToolkit)Toolkit.getDefaultToolkit()).realSync();
    }
}
