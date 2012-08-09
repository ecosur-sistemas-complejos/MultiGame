/*
* Copyright (C) 2011 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.manantiales.entity;

import mx.ecosur.multigame.grid.comparator.MoveComparator;
import mx.ecosur.multigame.grid.entity.GridMove;
import mx.ecosur.multigame.manantiales.enums.Mode;
import org.hibernate.annotations.Sort;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author awaterma@ecosur.mx
 */
@Entity
public class MoveHistory implements Serializable {

    private int id;

    private Mode mode;

    private Set<GridMove> moves;

    @Id
    @GeneratedValue
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    @ManyToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    @Sort(comparator=MoveComparator.class)
    public Set<GridMove> getMoves() {
        if (moves == null)
            moves = new TreeSet<GridMove>(new MoveComparator());
        return moves;
    }

    public void setMoves(Set<GridMove> moves) {
        this.moves = moves;
    }
}
