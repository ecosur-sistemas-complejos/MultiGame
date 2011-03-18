/*
* Copyright (C) 2011 ECOSUR, Andrew Waterman and Max Pimm
*
* Licensed under the Academic Free License v. 3.0.
* http://www.opensource.org/licenses/afl-3.0.php
*/

package mx.ecosur.multigame.impl.entity.manantiales;

import mx.ecosur.multigame.grid.model.GridMove;
import mx.ecosur.multigame.impl.enums.manantiales.Mode;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

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

    @OneToMany(cascade={CascadeType.ALL}, fetch=FetchType.EAGER)
    public Set<GridMove> getMoves() {
        return moves;
    }

    public void setMoves(Set<GridMove> moves) {
        this.moves = moves;
    }
}
