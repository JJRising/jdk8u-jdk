/*
 * Copyright 2004 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
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

package sun.tools.jstat;

import sun.jvmstat.monitor.*;
import sun.management.counter.Variability;

/**
 * A class implementing the ExpressionEvaluator to resolve unresolved
 * symbols in an Expression in the context of the available monitoring data.
 * This class also performs some minimal optimizations of the expressions,
 * such as simplification of constant subexpressions.
 *
 * @author Brian Doherty
 * @since 1.5
 */
public class ExpressionResolver implements ExpressionEvaluator {
    private static boolean debug = Boolean.getBoolean("ExpressionResolver.debug");
    private MonitoredVm vm;

    ExpressionResolver(MonitoredVm vm) {
        this.vm = vm;
    }

    /*
     * evaluate the given expression. evaluation in this case means
     * to resolve the counter names in the expression
     */
    public Object evaluate(Expression e) throws MonitorException {

        if (e == null) {
            return null;
        }

        if (debug) {
            System.out.println("Resolving Expression:" + e);
        }

        if (e instanceof Identifier) {
            Identifier id = (Identifier)e;

            // check if it's already resolved
            if (id.isResolved()) {
                return id;
            }

            // look it up
            Monitor m = vm.findByName(id.getName());
            if (m == null) {
                System.err.println("Warning: Unresolved Symbol: "
                                   + id.getName() + " substituted NaN");
                return new Literal(new Double(Double.NaN));
            }
            if (m.getVariability() == Variability.CONSTANT) {
                if (debug) {
                    System.out.println("Converting constant " + id.getName()
                                       + " to literal with value "
                                       + m.getValue());
                }
                return new Literal(m.getValue());
            }
            id.setValue(m);
            return id;
        }

        if (e instanceof Literal) {
            return e;
        }

        Expression l = null;
        Expression r = null;

        if (e.getLeft() != null) {
            l = (Expression)evaluate(e.getLeft());
        }
        if (e.getRight() != null) {
            r = (Expression)evaluate(e.getRight());
        }

        if (l != null && r != null) {
            if ((l instanceof Literal) && (r instanceof Literal)) {
                Literal ll = (Literal)l;
                Literal rl = (Literal)r;
                boolean warn = false;

                Double nan = new Double(Double.NaN);
                if (ll.getValue() instanceof String) {
                    warn = true; ll.setValue(nan);
                }
                if (rl.getValue() instanceof String) {
                    warn = true; rl.setValue(nan);
                }
                if (debug && warn) {
                     System.out.println("Warning: String literal in "
                                        + "numerical expression: "
                                        + "substitutied NaN");
                }

                // perform the operation
                Number ln = (Number)ll.getValue();
                Number rn = (Number)rl.getValue();
                double result = e.getOperator().eval(ln.doubleValue(),
                                                     rn.doubleValue());
                if (debug) {
                    System.out.println("Converting expression " + e
                                       + " (left = " + ln.doubleValue() + ")"
                                       + " (right = " + rn.doubleValue() + ")"
                                       + " to literal value " + result);
                }
                return new Literal(new Double(result));
            }
        }

        if (l != null && r == null) {
            return l;
        }

        e.setLeft(l);
        e.setRight(r);

        return e;
    }
}
