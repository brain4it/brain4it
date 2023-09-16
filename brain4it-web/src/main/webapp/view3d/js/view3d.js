if (!Detector.webgl)
  Detector.addGetWebGLMessage();

var container, clock, controls;
var camera, scene, renderer;
var position = null, quaternion = null;
var componentMaterial = new THREE.MeshPhongMaterial(
{ color: 0xff3355, specular: 0x111111, shininess: 200 });
var printedPartMaterial = new THREE.MeshPhongMaterial(
{ color: 0x3355ff, specular: 0x111111, shininess: 200 });
var selectedObject = null;
var selectPoint = null;
var DRAG_PIXELS = 5;
var exporter = new THREE.STLExporter();

init();
animate();

function init()
{
  container = document.getElementById('container');

  camera = new THREE.PerspectiveCamera(25,
    container.clientWidth / container.clientHeight, 0.1, 1000);
  camera.up.set(0, 0, 1);
  camera.position.set(0, -0.5, 0.25);

  scene = new THREE.Scene();

  var center = new THREE.Vector3();

  var href = window.location.href;
  var url = new URL(href);
  var modelPath = url.searchParams.get("model");
  if (modelPath.lastIndexOf(".stl") !== -1)
  {
    var loader = new THREE.STLLoader();
    loader.load('../' + modelPath, function (geometry)
    {
      geometry.computeBoundingBox();
      geometry.boundingBox.getCenter(center);
      var material = new THREE.MeshPhongMaterial(
              {color: 0x3355ff, specular: 0x111111, shininess: 200});
      var mesh = new THREE.Mesh(geometry, material);
      mesh.position.set(0, 0, 0);
      mesh.scale.set(0.001, 0.001, 0.001);
      mesh.castShadow = true;
      mesh.receiveShadow = true;
      scene.add(mesh);
      repaint();
      document.getElementById("loading").style.display = "none";
    });
  }
  else if (modelPath.lastIndexOf(".json") !== -1)
  {
    var loader = new THREE.CJSONLoader();
    loader.load('../' + modelPath, function (object)
    {
      scene.add(object);
      repaint();
      document.getElementById("loading").style.display = "none";
    });
  }
  var gridHelper = new THREE.GridHelper(1, 10,
    new THREE.Color(0x8080a0), new THREE.Color(0x404040));
  gridHelper.rotation.set(-Math.PI / 2, 0, 0);
  scene.add(gridHelper);

  var ambientLight = new THREE.AmbientLight(0xffffff, 0.2);
  scene.add(ambientLight);

  var pointLight = new THREE.DirectionalLight(0xffffff, 0.5);
  pointLight.position.set(30, 30, 30);
  scene.add(pointLight);

  var pointLight2 = new THREE.DirectionalLight(0xffffff, 0.5);
  pointLight2.position.set(-30, 0, 10);
  scene.add(pointLight2);

  var pointLight3 = new THREE.DirectionalLight(0xffffff, 0.5);
  pointLight3.position.set(600, -300, 0);
  scene.add(pointLight3);

  renderer = new THREE.WebGLRenderer({antialias: true});
  renderer.setPixelRatio(window.devicePixelRatio);
  renderer.setSize(container.clientWidth, container.clientHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;
  container.appendChild(renderer.domElement);

  controls = new THREE.OrbitControls(camera, renderer.domElement);
  controls.target.set(0, 0, 0);
  controls.update();

  window.addEventListener('resize', onWindowResize, false);

  container.addEventListener('mousedown', onMouseDown, false);
  container.addEventListener('mousemove', onMouseMove, false);
  container.addEventListener('mouseup', onMouseUp, false);

  container.addEventListener('touchstart', onTouchStart, false);
  container.addEventListener('touchmove', onTouchMove, false);
  container.addEventListener('touchend', onTouchEnd, false);

  document.getElementById("loading").style.display = "block";
}

function onWindowResize() 
{
  camera.aspect = container.clientWidth / container.clientHeight;
  camera.updateProjectionMatrix();

  renderer.setSize(container.clientWidth, container.clientHeight);
  repaint();
}

function onMouseDown(event)
{
  event.preventDefault();
  selectPoint = {x: event.clientX, y: event.clientY};
}

function onMouseMove(event)
{
  event.preventDefault();
  if (selectPoint !== null && 
      (Math.abs(selectPoint.x - event.clientX) > DRAG_PIXELS || 
       Math.abs(selectPoint.y - event.clientY) > DRAG_PIXELS))
  {
    selectPoint = null;
  }
}

function onMouseUp(event)
{
  if (selectPoint)
  {
    selectByPoint(selectPoint.x, selectPoint.y);
  }
}

function onTouchStart(event)
{
  event.preventDefault();
  var touchEvent = event.touches[0];
  selectPoint = {x: touchEvent.pageX, y: touchEvent.pageY};
}

function onTouchMove(event)
{
  event.preventDefault();
  var touchEvent = event.touches[0];
  if (selectPoint !== null &&
      (Math.abs(selectPoint.x - touchEvent.pageX) > DRAG_PIXELS || 
       Math.abs(selectPoint.y - touchEvent.pageY) > DRAG_PIXELS))
  {
    selectPoint = null;
  }
}

function onTouchEnd(event)
{
  event.preventDefault();
  if (selectPoint !== null)
  {
    selectByPoint(selectPoint.x, selectPoint.y);
  }
}

function paintSelection(object, material)
{
  if (material)
  {
    object.previousMaterial = object.material;
    object.material = material;
  }
  else if (object.previousMaterial)
  {
    object.material = object.previousMaterial;
    delete object.previousMaterial;      
  }
  for (var i = 0; i < object.children.length; i++)
  {
    var child = object.children[i];
    paintSelection(child, material);
  }
}

function selectByPoint(screenX, screenY)
{
  if (selectedObject)
  {
    paintSelection(selectedObject, null);
  }
  selectedObject = null;
  document.getElementById("selection").innerHTML = "";
  
  var rect = container.getBoundingClientRect();
  var x = screenX - rect.left;
  var y = screenY - rect.top;

  var vector = new THREE.Vector3(
    (x / rect.width) * 2 - 1, 
   -(y / rect.height) * 2 + 1, 0.5);
  vector = vector.unproject(camera);
  var raycaster = new THREE.Raycaster(
    camera.position, vector.sub(camera.position).normalize());
  var intersects = raycaster.intersectObject(scene, true);
  var i = 0;
  while (i < intersects.length)
  {
    var object = intersects[i].object;
    if (object.type === "Mesh")
    {
      selectedObject = object;
      break;
    }
    i++;
  }
  document.getElementById("download_stl").style.display = "none";
  if (selectedObject)
  {
    selectedObject = expandSelection(selectedObject);
    if (selectedObject)
    {
      var material;
      if (selectedObject.userData.type === "printed_part")
      {
        document.getElementById("download_stl").style.display = "block";
        material = printedPartMaterial;        
      }
      else
      {
        material = componentMaterial;        
      }      
      paintSelection(selectedObject, material);
      document.getElementById("selection").innerHTML = 
        selectedObject.userData.type + " " + selectedObject.name;
    }
  }
  repaint();
}

function showAll()
{
  setVisibility(scene, true);
  repaint();
}

function hideSelection()
{
  if (selectedObject)
  {
    setVisibility(selectedObject, false);
    repaint();
  }
}

function hideOther()
{
  if (selectedObject)
  {
    setVisibility(scene, false);
    setVisibility(selectedObject, true);
    repaint();
  }
}

function showByType(object, type)
{
  console.info(object.name);
  var objectType = object.userData.type;
  if (objectType !== undefined)
  {
    if (objectType === type)
    {
      setVisibility(object, true);
    }
    else
    {
      setVisibility(object, false);
    }
  }
  else
  {
    for (var i = 0; i < object.children.length; i++)
    {
      var child = object.children[i];
      showByType(child, type);
    }    
  }
  repaint();
}

function setVisibility(object, visible)
{
  if (object.type === "Mesh")
  {
    object.visible = visible;
  }
  for (var i = 0; i < object.children.length; i++)
  {
    var child = object.children[i];
    setVisibility(child, visible);
  }
}

function setCamera(x, y, z)
{
  camera.position.set(x, y, z);
  camera.lookAt(0, 0, 0);
  controls.update();
  repaint();
}

function expandSelection(object)
{
  // look for object with type property in userData
  var type = object.userData.type;
  while (object !== null && type === undefined)
  {
    object = object.parent;
    type = object ? object.userData.type : null;
  }
  return object;
}

function downloadSTL()
{
  if (selectedObject)
  {
    var result = exporter.parse(selectedObject);
    saveString(result, selectedObject.name + ".stl");
  }
  else
  {
    alert("Select part to download");
  }
}

function saveString(text, filename)
{
  var blob = new Blob([text], {type: 'text/plain'});
  var link = document.getElementById("download_link");
  link.href = URL.createObjectURL(blob);
  link.download = filename;
  link.click();
}

function repaint()
{
  position = null; // camera position change to force repaint  
}

function animate() 
{
  requestAnimationFrame(animate);

  if (position === null ||
      quaternion === null ||
      !position.equals(camera.position) || 
      !quaternion.equals(camera.quaternion))
  {
    renderer.render(scene, camera);
    position = camera.position.clone();
    quaternion = camera.quaternion.clone();
  }  
}

