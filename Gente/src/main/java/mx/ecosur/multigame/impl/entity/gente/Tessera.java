package mx.ecosur.multigame.impl.entity.gente;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.model.GridCell;
import org.hibernate.annotations.Sort;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

@Entity
public class Tessera implements Serializable {

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

    public void setCells(Set<GridCell> cells) {
        if (cells != null && cells.size() > 4)
            throw new RuntimeException("Unable to create tessera with " + cells.size() + " cells!");
        this._cells = cells;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Tessera clone = new Tessera();
        clone.setId(this.getId());
        if (getCells() != null) {
            Set<GridCell> cc = new TreeSet<GridCell>(new CellComparator());
            for (GridCell cell : getCells()) {
                cc.add(cell.clone());
            }
            clone.setCells(cc);
        }

        return clone;

    }
}
