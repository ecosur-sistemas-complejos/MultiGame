package mx.ecosur.multigame.impl.entity.gente;

import mx.ecosur.multigame.grid.comparator.CellComparator;
import mx.ecosur.multigame.grid.entity.GridCell;
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
<<<<<<< HEAD:Gente/GenteModel/src/main/java/mx/ecosur/multigame/impl/entity/gente/Tria.java
<<<<<<< HEAD:Gente/GenteModel/src/main/java/mx/ecosur/multigame/impl/entity/gente/Tria.java
=======
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

>>>>>>> Minor changes to Tria and Tessera entities, updates to Flex modules to be a) more explicit about variable to services-config.xml and b) correct issue in FlexMultigame where services was not being passed to the compiler by FlexMojos.:Gente/src/main/java/mx/ecosur/multigame/impl/entity/gente/Tria.java
=======
>>>>>>> Fixes for Manantiales (games couldn't load) and updates to Tria and Tessera code for Gente in order to avoid Hibernate's lazy instantiation exceptions on calling size().:Gente/src/main/java/mx/ecosur/multigame/impl/entity/gente/Tria.java
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
