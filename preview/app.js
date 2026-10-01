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