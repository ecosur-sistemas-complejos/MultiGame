/**
 * Created by IntelliJ IDEA.
 * User: awaterma
 * Date: 4/6/11
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */
package mx.ecosur.multigame.gente.entity {

    import mx.collections.ArrayCollection;

    [RemoteClass (alias="mx.ecosur.multigame.gente.entity.Tessera")]
    public class Tessera {

        public var id:int;

        public var cells:ArrayCollection;

        public function Tessera() {
            super();
        }
    }
}
