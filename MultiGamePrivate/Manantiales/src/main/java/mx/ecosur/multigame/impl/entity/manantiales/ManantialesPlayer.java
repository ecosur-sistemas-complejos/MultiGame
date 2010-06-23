/*
* Copyright (C) 2008, 2009 ECOSUR, Andrew Waterman
*
* Licensed under the Academic Free License v. 3.2.
* http://www.opensource.org/licenses/afl-3.0.php
*/

/**
 * @author awaterma@ecosur.mx
 */
package mx.ecosur.multigame.impl.entity.manantiales;

import javax.persistence.Entity;

import mx.ecosur.multigame.impl.Color;
import mx.ecosur.multigame.impl.model.GridPlayer;
import mx.ecosur.multigame.impl.model.GridRegistrant;

@Entity
public class ManantialesPlayer extends GridPlayer {

    private static final long serialVersionUID = 2815240265995571202L;

    private int points, forested, moderate, intensive, vivero, silvo;

    public ManantialesPlayer () {
        super();
    }

    public ManantialesPlayer (GridRegistrant player, Color favoriteColor) {
        super (player, favoriteColor);
        points = 0;
    }

    public int getScore() {
        return points;
    }

    public void setScore (int score) {
        this.points = score;
    }

    /**
     * @return the forested
     */
    public int getForested() {
        return forested;
    }

    /**
     * @return the moderate
     */
    public int getModerate() {
        return moderate;
    }

    /**
     * @return the intensive
     */
    public int getIntensive() {
        return intensive;
    }

    /**
     * @return the vivero
     */
    public int getVivero() {
        return vivero;
    }

    /**
     * @return the silvo
     */
    public int getSilvo() {
        return silvo;
    }

    /**
     * @param forested the forested to set
     */
    public void setForested(int forested) {
        if (forested > 0)
            this.forested = forested;
        else
            this.forested = 0;
    }

    /**
     * @param moderate the moderate to set
     */
    public void setModerate(int moderate) {
        if (moderate > 0)
            this.moderate = moderate;
        else
            this.moderate = 0;
    }

    /**
     * @param intensive the intensive to set
     */
    public void setIntensive(int intensive) {
        if (intensive > 0)
            this.intensive = intensive;
        else
            this.intensive = 0;
    }

    /**
     * @param vivero the vivero to set
     */
    public void setVivero(int vivero) {
        if (vivero > 0)
            this.vivero = vivero;
        else
            vivero = 0;
    }

    /**
     * @param silvo the silvo to set
     */
    public void setSilvo(int silvo) {
        if (silvo > 0)
            this.silvo = silvo;
        else
            this.silvo = 0;
    }

    public void reset() {
        this.forested = 0;
        this.intensive = 0;
        this.moderate = 0;
        this.silvo = 0;
        this.vivero = 0;
        this.points = 0;
    }
}
