package mx.ecosur.multigame.impl.entity.gente;

import mx.ecosur.multigame.grid.Color;
import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.model.GridCell;
import org.hibernate.annotations.Sort;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

@Entity
public class Tria implements Serializable {

    private int _id;

    private Set<GridCell> _cells;

    @Id
    @GeneratedValue
    public int getId() {
        return _id;
    }

    public void setId(int id) {
        this._id = id;
    }

    @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE}, fetch= FetchType.EAGER)
    @Sort(comparator=CellComparator.class)
    public Set<GridCell> getCells() {
        if (_cells == null)
            _cells = new TreeSet<GridCell>(new CellComparator());
        return _cells;
    }

    public void setCells(Set<GridCell> cells) throws Exception {
        if (cells.size() > 3)
            throw new RuntimeException ("Unable to create Tria with " + cells.size() + " cells!");
        Color master = null;
        for (GridCell cell : cells) {
            if (master == null)
                master = cell.getColor();
            if (!cell.getColor().equals(master))
                throw new Exception ("Trias can only be of the same color [" + master +
                        " != " + cell.getColor() + "]!");

        }
        this._cells = cells;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Tria clone = new Tria();
        clone.setId(this.getId());
        if (getCells() != null) {
            Set<GridCell> cc = new TreeSet<GridCell>(new CellComparator());
            for (GridCell cell : getCells()) {
                cc.add(cell.clone());
            }
            try {
                clone.setCells(cc);
            } catch (Exception e) {
                clone = null;
            }
        }

        return clone;

    }

}
