export const demoClasses = [
  ['React로 만드는 나의 첫 웹 서비스','컴포넌트부터 상태 관리까지, 직접 만들며 배우는 React. 나만의 웹 서비스를 완성해 보세요.',89000,30,'OPEN','react'],
  ['Spring Boot, 백엔드의 시작','REST API 설계부터 데이터베이스 연동까지. 서비스의 기반을 탄탄하게 만드는 시간입니다.',120000,25,'OPEN','spring'],
  ['일상을 기록하는 감각적인 UI 디자인','사용자의 경험을 생각하고, 아이디어를 화면으로 표현하는 디자인 수업입니다.',65000,20,'OPEN','design'],
  ['Python으로 시작하는 데이터 분석','데이터를 읽고 정리하며 나만의 인사이트를 발견해 보세요.',79000,30,'OPEN','python'],
  ['처음 만나는 데이터베이스와 SQL','데이터의 구조를 이해하고 필요한 정보를 정확하게 찾는 법을 배웁니다.',55000,35,'DRAFT','sql'],
  ['Git & GitHub, 함께 만드는 코드','브랜치와 코드 리뷰로 더 나은 협업 방식을 익혀 보세요.',45000,20,'CLOSED','git'],
].map(([classTitle,classContent,classPrice,classMaxCap,classState,art],i)=>({classSeq:i+1,classTitle,classContent,classPrice,classMaxCap,classState,art,classCurrApps:8+i*2,classStartDate:'2026-09-01T09:00:00',classEndDate:'2026-10-30T18:00:00'}));
