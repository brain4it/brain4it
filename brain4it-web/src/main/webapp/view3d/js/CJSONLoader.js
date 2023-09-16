/**
 * Compact JSON loader
 */


THREE.CJSONLoader = function (manager) 
{
  this.manager = (manager !== undefined) ? 
    manager : THREE.DefaultLoadingManager;
};

THREE.CJSONLoader.prototype = 
{
  constructor: THREE.CJSONLoader,

  load: function (url, onLoad, onProgress, onError) 
  {
    var scope = this;

    var loader = new THREE.FileLoader(scope.manager);
    loader.load(url, function (text) 
    {
      try
      {
        onLoad(scope.parse(JSON.parse(text)));
      } 
      catch (exception)
      {
        if (onError)
        {
          onError(exception);
        }
      }
    }, onProgress, onError);
  },

  parse: function (json)
  {
    var geometries = [];
    
    for (var i = 0; i < json.geometries.length; i++)
    {
      var geometry = new THREE.BufferGeometry();
      geometries[i] = geometry;

      var jsonGeometry = json.geometries[i];
      var pointIndexs = jsonGeometry.points;
      var normalIndexs = jsonGeometry.normals;
      
      var vertices = [];
      var normals = [];
      
      for (var j = 0; j < pointIndexs.length; j++)
      {
        var v = json.vectors[pointIndexs[j]];
        vertices.push(v[0]);
        vertices.push(v[1]);
        vertices.push(v[2]);
        
        var n = json.vectors[normalIndexs[j]];
        normals.push(n[0]);
        normals.push(n[1]);
        normals.push(n[2]);
      }

      geometry.addAttribute('position', 
        new THREE.BufferAttribute(new Float32Array(vertices), 3));
      geometry.addAttribute('normal', 
        new THREE.BufferAttribute(new Float32Array(normals), 3));
    }
    
    var createObject = function(jsonNode, parentMaterial)
    {
      var object;
      var material;
      var mesh = null;
      
      if (jsonNode.color !== undefined)
      {
        var red = jsonNode.color[0];
        var green = jsonNode.color[1];
        var blue = jsonNode.color[2];
        var color = new THREE.Color(red / 255.0, green / 255.0, blue / 255.0);
        material = new THREE.MeshPhongMaterial( 
          {color: color, specular: 0x111111, shininess: 200});
      }
      else
      {
        material = parentMaterial;
      }
      if (jsonNode.geometry !== undefined)
      {
        mesh = new THREE.Mesh(geometries[jsonNode.geometry], material);
        mesh.receiveShadow = true;
        mesh.castShadow = true;
      }
      if (jsonNode.children.length > 0)
      {
        object = new THREE.Group();
        if (mesh !== null)
        {
          object.add(mesh);
        }
      }
      else if (mesh !== null)
      {
        object = mesh;
      }
      else
      {
        object = new THREE.Group();
      }
      object.name = jsonNode.name;
      
      if (jsonNode.metadata)
      {
        object.userData = jsonNode.metadata;
      }
      
      var jsonMatrix = jsonNode.matrix;
      var array = [];
      for (var m = 0; m < jsonMatrix.length; m++)
      {
        var vm = json.vectors[jsonMatrix[m]];
        array.push(vm[0]);
        array.push(vm[1]);
        array.push(vm[2]);
        array.push(m === 3 ? 1 : 0);
      }
      var matrix = new THREE.Matrix4();
      matrix.fromArray(array);
      matrix.decompose(object.position, object.quaternion, object.scale);
      
      for (var i = 0; i < jsonNode.children.length; i++)
      {
        var child = createObject(jsonNode.children[i], material);
        object.add(child);
      }
      return object;
    };
    
    return createObject(json.scene, new THREE.MeshPhongMaterial(
      { color: 0x3355ff, specular: 0x111111, shininess: 200 })); 
  }
};
