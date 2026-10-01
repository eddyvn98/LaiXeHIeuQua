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

const DIGIT_SEGMENTS={
  "0":["a","b","c","d","e","f"],
  "1":["b","c"],
  "2":["a","b","d","e","g"],
  "3":["a","b","c","d","g"],
  "4":["b","c","f","g"],
  "5":["a","c","d","f","g"],
  "6":["a","c","d","e","f","g"],
  "7":["a","b","c"],
  "8":["a","b","c","d","e","f","g"],
  "9":["a","b","c","d","f","g"],
};

function renderSevenDisplay(container,text){
  if(!container)return;
  container.replaceChildren();
  const chars=String(text).split("");
  chars.forEach((char,index)=>{
    if(char===":"){
      const colon=document.createElement("span");
      colon.className="seven-colon";
      container.appendChild(colon);
      return;
    }
    if(char===".")return;

    const digit=document.createElement("span");
    digit.className="seg-digit";
    const active=new Set(DIGIT_SEGMENTS[char]||[]);
    ["a","b","c","d","e","f","g"].forEach(name=>{
      const seg=document.createElement("span");
      seg.className="segment "+(["a","g","d"].includes(name)?"h ":"v ")+name+(active.has(name)?" on":"");
      digit.appendChild(seg);
    });
    container.appendChild(digit);

    if(chars[index+1]==="."){
      const dot=document.createElement("span");
      dot.className="decimal-dot";
      container.appendChild(dot);
    }
  });
}

function buildSmoothPath(points){
  if(points.length===0)return "";
  if(points.length===1)return "M "+points[0].x+" "+points[0].y;
  let d="M "+points[0].x.toFixed(1)+" "+points[0].y.toFixed(1);
  for(let i=0;i<points.length-1;i++){
    const p0=points[Math.max(0,i-1)];
    const p1=points[i];
    const p2=points[i+1];
    const p3=points[Math.min(points.length-1,i+2)];
    const c1x=p1.x+(p2.x-p0.x)/6;
    const c1y=p1.y+(p2.y-p0.y)/6;
    const c2x=p2.x-(p3.x-p1.x)/6;
    const c2y=p2.y-(p3.y-p1.y)/6;
    d+=" C "+c1x.toFixed(1)+" "+c1y.toFixed(1)+", "+c2x.toFixed(1)+" "+c2y.toFixed(1)+", "+p2.x.toFixed(1)+" "+p2.y.toFixed(1);
  }
  return d;
}

function renderAdaptiveChart({values,pathId,currentDotId,currentValueId,minId,midId,maxId,scaleId,padding,step,suffix,height=90,decimals=0}){
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
  const points=values.map((value,index)=>({
    x:values.length===1?0:index/(values.length-1)*width,
    y:height-((value-min)/(max-min))*height,
    value,
  }));
  document.getElementById(pathId).setAttribute("d",buildSmoothPath(points));
  const current=points[points.length-1];
  const dot=document.getElementById(currentDotId);
  dot.setAttribute("cx",current.x.toFixed(1));
  dot.setAttribute("cy",current.y.toFixed(1));
  document.getElementById(currentValueId).textContent=
    values[values.length-1].toFixed(decimals)+(suffix?" "+suffix:"");
  document.getElementById(minId).textContent=roundNice(min,step);
  document.getElementById(midId).textContent=roundNice(mid,step);
  document.getElementById(maxId).textContent=roundNice(max,step);
  document.getElementById(scaleId).textContent=
    roundNice(min,step)+"–"+roundNice(max,step)+(suffix?" "+suffix:"")+" · AUTO SCALE";
}

const recentSpeed=[39,41,40,42,44,43,45,47,46,44,43,45,46,48,47,45,44,42,41,43,44,43];
const recentEfficiency=[84,86,85,87,88,86,89,91,90,92,89,88,90,91,89,87,88,90,89,91,90,88];

renderSevenDisplay(document.getElementById("gpsSpeedLed"),"42.7");

renderAdaptiveChart({
  values:recentSpeed,
  pathId:"speedPath",
  currentDotId:"speedCurrentDot",
  currentValueId:"speedCurrentValue",
  minId:"speedMinLabel",
  midId:"speedMidLabel",
  maxId:"speedMaxLabel",
  scaleId:"speedScaleLabel",
  padding:1,
  step:2,
  suffix:"km/h",
  height:90,
  decimals:0,
});

renderAdaptiveChart({
  values:recentEfficiency,
  pathId:"effPath",
  currentDotId:"effCurrentDot",
  currentValueId:"effCurrentValue",
  minId:"effMinLabel",
  midId:"effMidLabel",
  maxId:"effMaxLabel",
  scaleId:"effScaleLabel",
  padding:1,
  step:2,
  suffix:"",
  height:120,
  decimals:0,
});

const tripDuration=document.getElementById("tripDuration");
const mainGauge=document.querySelector(".main-gauge");
const speedAction=document.querySelector(".speed-action");
const liveStatus=document.getElementById("liveStatus");

let rideRunning=true;
let rideStartedAt=Date.now()-(18*60+42)*1000;
let rideElapsedMs=0;

function formatDuration(ms){
  const totalSeconds=Math.max(0,Math.floor(ms/1000));
  const hours=Math.floor(totalSeconds/3600);
  const minutes=Math.floor((totalSeconds%3600)/60);
  const seconds=totalSeconds%60;
  return [hours,minutes,seconds].map(v=>String(v).padStart(2,"0")).join(":");
}

function updateClockAndRide(){
  const now=new Date();
  renderSevenDisplay(
    document.getElementById("digitalClock"),
    now.toLocaleTimeString("vi-VN",{hour:"2-digit",minute:"2-digit",hour12:false})
  );
  const elapsed=rideRunning?Date.now()-rideStartedAt:rideElapsedMs;
  tripDuration.textContent=formatDuration(elapsed);
}
setInterval(updateClockAndRide,1000);
updateClockAndRide();

mainGauge.addEventListener("click",()=>{
  if(rideRunning){
    rideElapsedMs=Date.now()-rideStartedAt;
    rideRunning=false;
    speedAction.textContent="CHẠM ĐỂ BẮT ĐẦU";
    liveStatus.innerHTML="<span></span> KẾT THÚC";
    liveStatus.classList.add("stopped");
  }else{
    rideStartedAt=Date.now();
    rideElapsedMs=0;
    rideRunning=true;
    speedAction.textContent="CHẠM ĐỂ DỪNG";
    liveStatus.innerHTML="<span></span> LIVE";
    liveStatus.classList.remove("stopped");
  }
  updateClockAndRide();
});
