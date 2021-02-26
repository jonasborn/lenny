package de.jonasborn.lenny

import de.vandermeer.asciitable.AT_Context
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.asciithemes.a8.A8_Grids
import de.vandermeer.asciithemes.u8.U8_Grids

class Table {

    private AsciiTable at

    static Table create() {
        return new Table();
    }

    Table() {

        def context = new AT_Context();
        context.setGrid(A8_Grids.lineDobuleTripple())
        at = new AsciiTable(context);

    }

    public Table strong() {
        at.addStrongRule()
        return this
    }

    public Table heavy() {
        at.addHeavyRule()
        return this
    }

    public Table normal() {
        at.addRule()
        return this
    }

    public Table title(Object... objects) {
        at.addHeavyRule()
        at.addRow(objects)
        at.addHeavyRule()
        return this
    }

    public Table header(Object... objects) {
        at.addStrongRule()
        at.addRow(objects)
        at.addStrongRule()
        return this
    }

    public Table add(Object... objects) {
        at.addRow(objects.collect { it.toString() })
        return this
    }

    public Table addRow(Object... objects) {
        add(objects)
        at.addRule()
        return this
    }

    public Table render() {
        println at.render()
        return this
    }
}
