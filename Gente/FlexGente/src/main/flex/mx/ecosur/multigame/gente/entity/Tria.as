/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 4/6/11
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.multigame.gente.entity  {

    import mx.collections.ArrayCollection;

    [RemoteClass (alias="mx.ecosur.multigame.gente.entity.Tria")]
    public class Tria {

        public var id:int;

        public var cells:ArrayCollection;

        public function Tria() {
            super();
        }

    }
}
