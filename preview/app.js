const odoBtn=document.getElementById("odoBtn");
const fuelBtn=document.getElementById("fuelBtn");
const odoDialog=document.getElementById("odoDialog");
const fuelDialog=document.getElementById("fuelDialog");
const odoInput=document.getElementById("odoInput");
const fuelOdo=document.getElementById("fuelOdo");
const odoValue=document.getElementById("odoValue");

odoBtn.addEventListener("click",()=>odoDialog.showModal());
fuelBtn.addEventListener("click",()=>{
  fuelOdo.value=odoInput.value;
  recalcFuel();
  fuelDialog.showModal();
});

document.getElementById("saveOdo").addEventListener("click",event=>{
  event.preventDefault();
  const value=Number(odoInput.value);
  if(!Number.isFinite(value)||value<0)return;
  odoValue.textContent=value.toLocaleString("en-US",{minimumFractionDigits:1,maximumFractionDigits:1});
  fuelOdo.value=value.toFixed(1);
  odoDialog.close();
});

const price=document.getElementById("unitPrice");
const amount=document.getElementById("amountPaid");
const capacity=document.getElementById("tankCapacity");
const full=document.getElementById("fullTank");
const litersCalc=document.getElementById("litersCalc");
const afterFill=document.getElementById("afterFill");

function recalcFuel(){
  const p=Number(price.value);
  const a=Number(amount.value);
  const cap=Number(capacity.value);
  const liters=p>0&&a>0?a/p:0;
  litersCalc.textContent=liters>0?liters.toFixed(2)+" L":"—";
  if(full.checked&&cap>0){
    afterFill.textContent=cap.toFixed(2)+" L trong bình";
  }else if(liters>0){
    afterFill.textContent="+ "+liters.toFixed(2)+" L được ghi nhận";
  }else{
    afterFill.textContent="—";
  }
}
[price,amount,capacity,full].forEach(el=>el.addEventListener("input",recalcFuel));
full.addEventListener("change",recalcFuel);

document.getElementById("saveFuel").addEventListener("click",event=>{
  event.preventDefault();
  const odo=Number(fuelOdo.value);
  const p=Number(price.value);
  const a=Number(amount.value);
  const cap=Number(capacity.value);
  if(!(odo>=0&&p>0&&a>0))return;

  odoInput.value=odo.toFixed(1);
  odoValue.textContent=odo.toLocaleString("en-US",{minimumFractionDigits:1,maximumFractionDigits:1});

  const liters=a/p;
  const avg=49.3;
  const remain=full.checked&&cap>0?cap:Math.min(cap||99,4.7+liters);
  document.getElementById("fuelRemain").textContent=remain.toFixed(1)+" L";
  document.getElementById("rangeRemain").textContent=Math.round(remain*avg)+" km";
  fuelDialog.close();
});
recalcFuel();

function roundNice(value,step){
  return Math.round(value/step)*step;
}

function renderAdaptiveChart({values,polylineId,minId,midId,maxId,scaleId,padding,step,suffix,height=90}){
  const rawMin=Math.min(...values);
  const rawMax=Math.max(...values);
  let min=Math.floor((rawMin-padding)/step)*step;
  let max=Math.ceil((rawMax+padding)/step)*step;
  if(max-min<step*4){
    const center=(min+max)/2;
    min=center-step*2;
    max=center+step*2;
  }
  const mid=(min+max)/2;
  const width=900;
  const points=values.map((value,index)=>{
    const x=values.length===1?0:index/(values.length-1)*width;
    const y=height-((value-min)/(max-min))*height;
    return x.toFixed(1)+","+y.toFixed(1);
  }).join(" ");
  document.getElementById(polylineId).setAttribute("points",points);
  document.getElementById(minId).textContent=roundNice(min,step);
  document.getElementById(midId).textContent=roundNice(mid,step);
  document.getElementById(maxId).textContent=roundNice(max,step);
  document.getElementById(scaleId).textContent=
    roundNice(min,step)+"–"+roundNice(max,step)+(suffix?" "+suffix:"")+" · AUTO SCALE";
}

const recentSpeed=[39,41,40,42,44,43,45,47,46,44,43,45,46,48,47,45,44,42,41,43,44,43];
const recentEfficiency=[84,86,85,87,88,86,89,91,90,92,89,88,90,91,89,87,88,90,89,91,90,88];

renderAdaptiveChart({
  values:recentSpeed,
  polylineId:"speedPolyline",
  minId:"speedMinLabel",
  midId:"speedMidLabel",
  maxId:"speedMaxLabel",
  scaleId:"speedScaleLabel",
  padding:1,
  step:2,
  suffix:"km/h",
  height:90,
});

renderAdaptiveChart({
  values:recentEfficiency,
  polylineId:"effPolyline",
  minId:"effMinLabel",
  midId:"effMidLabel",
  maxId:"effMaxLabel",
  scaleId:"effScaleLabel",
  padding:1,
  step:2,
  suffix:"",
  height:120,
});