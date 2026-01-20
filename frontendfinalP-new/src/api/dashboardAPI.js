const BASE_URL = "http://localhost:8080/api/dashboard";

export async function fetchAvailableCourses(userId) {
  const res = await fetch(`${BASE_URL}/${userId}/available-courses`);
  if (!res.ok) throw new Error("Failed to load available courses");
  return res.json();
}

// export async function fetchProgress(userId) {
//   const res = await fetch(`${BASE_URL}/${userId}/progress`);
//   if (!res.ok) throw new Error("Failed to load progress");
//   return res.json();
// }

// export async function fetchRecommendations(userId) {
//   const res = await fetch(`${BASE_URL}/${userId}/recommendations`);
//   if (!res.ok) throw new Error("Failed to load recommendations");
//   return res.json();
// }
